.data

.data
# Allocated labels for virtual registers

.text
# Original instruction: j main
j main

.data
# Allocated labels for virtual registers
label_5_v0:
.space 4

.text
one:
# Original instruction: sw $fp,0($sp)
sw $fp,0($sp)
# Original instruction: add $fp,$zero,$sp
add $fp,$zero,$sp
# Original instruction: addi $sp,$sp,-4
addi $sp,$sp,-4
# Original instruction: addi $sp,$sp,-4
addi $sp,$sp,-4
# Original instruction: pushRegisters
la $t0,label_5_v0
lw $t0,0($t0)
addi $sp,$sp,-4
sw $t0,0($sp)
# Evaluating address of return value
# Loading register with int literal
# Original instruction: li v0,1
li $t5,1
la $t0,label_5_v0
sw $t5,0($t0)
# Transfering Value into stack
# Original instruction: sw v0,8($fp)
la $t5,label_5_v0
lw $t5,0($t5)
sw $t5,8($fp)
# Original instruction: j label_1_one_EndLabel
j label_1_one_EndLabel
label_1_one_EndLabel:
# Original instruction: popRegisters
lw $t0,0($sp)
addi $sp,$sp,4
la $t1,label_5_v0
sw $t0,0($t1)
# Original instruction: add $sp,$zero,$fp
add $sp,$zero,$fp
# Original instruction: lw $fp,0($sp)
lw $fp,0($sp)
# Original instruction: jr $ra
jr $ra

.data
# Allocated labels for virtual registers
label_7_v2:
.space 4

.text
main:
# Original instruction: sw $fp,0($sp)
sw $fp,0($sp)
# Original instruction: add $fp,$zero,$sp
add $fp,$zero,$sp
# Original instruction: addi $sp,$sp,-4
addi $sp,$sp,-4
# Original instruction: addi $sp,$sp,-4
addi $sp,$sp,-4
# Original instruction: pushRegisters
la $t0,label_7_v2
lw $t0,0($t0)
addi $sp,$sp,-4
sw $t0,0($sp)
# Function Call print_i
# Start of function call
# Original instruction: addi $sp,$sp,-8
addi $sp,$sp,-8
# PushReturnValue starts here
# Original instruction: sw $ra,4($sp)
sw $ra,4($sp)
# Calling function one
# Original instruction: jal one
jal one
# Function Call ended 
# Restoring ra
# Original instruction: lw $ra,4($sp)
lw $ra,4($sp)
# Original instruction: lw v2,8($sp)
lw $t5,8($sp)
la $t0,label_7_v2
sw $t5,0($t0)
# Original instruction: add $sp,$zero,$fp
add $sp,$zero,$fp
# Loading result into a0 and printing
# Original instruction: add $a0,$zero,v2
la $t5,label_7_v2
lw $t5,0($t5)
add $a0,$zero,$t5
# Original instruction: li $v0,1
li $v0,1
# Original instruction: syscall
syscall
label_3_main_EndLabel:
# Original instruction: popRegisters
lw $t0,0($sp)
addi $sp,$sp,4
la $t1,label_7_v2
sw $t0,0($t1)
# Original instruction: add $sp,$zero,$fp
add $sp,$zero,$fp
# Original instruction: lw $fp,0($sp)
lw $fp,0($sp)

