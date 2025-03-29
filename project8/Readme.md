**Handling Functions, Return, Call**

Maintain caller function's state.

Function state

working stack + memory segments

When function starts running:

1. State is Created when the function starts running.
2. Maintained as long as the function is executing.
3. Recycled when the function returns.

**Handling goto, if-goto, label**

**CodeWriter additional functionality**

1. writeInit
   Writes the assembly instructions that effect the bootstrap code
   that initializes the VM. This code must be placed at the beginning
   of the generalised \*.asm file.

2. writeLabel
   Arguments: label(string)
   Writes assembly code that effects the label command
3. writeGoto
   Arguments: label(string)
   Writes assembly code that effects the goto command.

4. writeIf
   Arguemnts: label(string)
   Writes assembly code that effects the if-goto command.

5. writeFunction
   Arguments: FunctionName(string)
   numVars (int)
   Writes assembly code that effects the function command

6. writeCall
   Arguments: functionName (string)
   numArgs (int)
   Writes assembly code that effects the call command

7. writeReturn
   Writes assembly code that effects the return command.
