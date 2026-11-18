# COA CO4 — ColdFlow Simulator

`co4.asm` is a standalone 64-bit NASM program demonstrating five COA CO4
concepts in the ColdFlow warehouse context:

1. Temperature error calculation and cooling-state selection.
2. FEFO selection between batches using expiry dates.
3. Robot route distance calculation and threshold checking.
4. Example control-unit signals for common instructions.
5. A five-stage instruction pipeline schedule.

## Build and run (Linux x86-64)

From this directory, assemble and link with NASM and GCC:

```sh
nasm -f elf64 co4.asm -o co4.o
gcc -no-pie co4.o -o co4
./co4
```

The simulator is an educational, standalone assembly demonstration. It does not
replace or connect to the Java application's runtime or the C++ temperature
engine. Its sample values are defined in the assembly source.
