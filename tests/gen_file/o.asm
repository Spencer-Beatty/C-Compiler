.data
label_1_string:
.asciiz "Hello world\n"
.space 3

.data
# Allocated labels for virtual registers

.text

.data
# Allocated labels for virtual registers
label_3_v0:
.space 4

.text
label_0_main:
# Function call print_s
# Original instruction: la v0,label_1_string
la $t5,label_1_string
la $t0,label_3_v0
sw $t5,0($t0)
# Original instruction: add $a0,$zero,v0
la $t5,label_3_v0
lw $t5,0($t5)
add $a0,$zero,$t5
# Original instruction: li $v0,4
li $v0,4
# Original instruction: syscall
syscall

