hwptr: hw
hw: 68
65
6C
6C
6F
20
77
6F
72
6C
64
0
start: NULL
loop1: LD (hwptr)
JZ $end1
OUT
LD $hwptr
INC
ST $hwptr
JUMP $loop1
end1: NULL
HLT
n: 0
hwptr: hw
hw: 68
65
6C
6C
6F
20
77
6F
72
6C
64
0
start: NULL
IN
ST $n
loop2: LOOP $n
JUMP $end2
LD $n
OUT
IN
ST $n
JUMP $loop2
end2: NULL
HLT