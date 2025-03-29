// write function SimpleFunction.test
(SimpleFunction.test)
// push constant 0
@0
D=A
@SP
AM=M+1
A=A-1
M=D
// push constant 0
@0
D=A
@SP
AM=M+1
A=A-1
M=D
// push local 0
@0
D=A
@LCL
A=D+M
D=M
@SP
AM=M+1
A=A-1
M=D
// push local 1
@1
D=A
@LCL
A=D+M
D=M
@SP
AM=M+1
A=A-1
M=D
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
// not
@SP
AM=M-1
M=!M
@SP
AM=M+1
// push argument 0
@ARG
D=M
@0
A=D+A
D=M
@SP
AM=M+1
A=A-1
M=D
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
// push argument 1
@ARG
D=M
@1
A=D+A
D=M
@SP
AM=M+1
A=A-1
M=D
// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D
// write return
@LCL
D=M
@R14
M=D
@5
A=D-A
D=M
@R15
M=D
// pop argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@ARG
D=M
@SP
M=D+1
@R14
A=M-1
D=M
@THAT
M=D
@R14
D=M
@2
A=D-A
D=M
@THIS
M=D
@R14
D=M
@3
A=D-A
D=M
@ARG
M=D
@R14
D=M
@4
A=D-A
D=M
@LCL
M=D
@R15
A=M
0;JMP
