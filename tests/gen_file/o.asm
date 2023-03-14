.data
label_0_i:

.data
# Allocated labels for virtual registers

.text

.data
# Allocated labels for virtual registers
label_8_v3:
.space 4
label_17_v6:
.space 4
label_4_v1:
.space 4
label_15_v5:
.space 4
label_5_v0:
.space 4
label_14_v4:
.space 4
label_9_v2:
.space 4

.text
label_1_main:
# Original instruction: sw $fp,-4($sp)
sw $fp,-4($sp)
# Original instruction: add $fp,$zero,$sp
add $fp,$zero,$sp
# Original instruction: addi v0,$fp,-4
addi $t5,$fp,-4
la $t0,label_5_v0
sw $t5,0($t0)
# Loading register with int literal
# Original instruction: li v1,10
li $t5,10
la $t0,label_4_v1
sw $t5,0($t0)
# Original instruction: sw v1,0(v0)
la $t5,label_4_v1
lw $t5,0($t5)
la $t4,label_5_v0
lw $t4,0($t4)
sw $t5,0($t4)
# Original instruction: la v2,label_0_i
la $t5,label_0_i
la $t0,label_9_v2
sw $t5,0($t0)
# Loading register with int literal
# Original instruction: li v3,20
li $t5,20
la $t0,label_8_v3
sw $t5,0($t0)
# Original instruction: sw v3,0(v2)
la $t5,label_8_v3
lw $t5,0($t5)
la $t4,label_9_v2
lw $t4,0($t4)
sw $t5,0($t4)
# Function Call print_i
# Loading address label_0_i into register
# Original instruction: la v4,label_0_i
la $t5,label_0_i
la $t0,label_14_v4
sw $t5,0($t0)
# Loading word from address into register
# Original instruction: lw v4,0(v4)
la $t4,label_14_v4
lw $t4,0($t4)
lw $t4,0($t4)
la $t0,label_14_v4
sw $t4,0($t0)
# Loading word from the stack
# Original instruction: lw v5,-4($fp)
lw $t5,-4($fp)
la $t0,label_15_v5
sw $t5,0($t0)
# Binary Operation
# Add
# Original instruction: add v6,v4,v5
la $t5,label_14_v4
lw $t5,0($t5)
la $t4,label_15_v5
lw $t4,0($t4)
add $t3,$t5,$t4
la $t0,label_17_v6
sw $t3,0($t0)
# Loading result into a0 and printing
# Original instruction: add $a0,$zero,v6
la $t5,label_17_v6
lw $t5,0($t5)
add $a0,$zero,$t5
# Original instruction: li $v0,1
li $v0,1
# Original instruction: syscall
syscall
# Original instruction: lw $sp,-4($fp)
lw $sp,-4($fp)

