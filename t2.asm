.data
# Allocated labels for virtual registers
label_4_v2:
.space 4
label_2_v0:
.space 4
label_3_v1:
.space 4

.text
# Original instruction: pushRegisters
la $t0,label_4_v2
lw $t0,0($t0)
addi $sp,$sp,-4
sw $t0,0($sp)
la $t0,label_2_v0
lw $t0,0($t0)
addi $sp,$sp,-4
sw $t0,0($sp)
la $t0,label_3_v1
lw $t0,0($t0)
addi $sp,$sp,-4
sw $t0,0($sp)
# Original instruction: addi v0,$zero,4
addi $t5,$zero,4
la $t0,label_2_v0
sw $t5,0($t0)
# Original instruction: addi v1,$zero,8
addi $t5,$zero,8
la $t0,label_3_v1
sw $t5,0($t0)
# Original instruction: add v2,v0,v1
la $t5,label_2_v0
lw $t5,0($t5)
la $t4,label_3_v1
lw $t4,0($t4)
add $t3,$t5,$t4
la $t0,label_4_v2
sw $t3,0($t0)
# Original instruction: popRegisters
lw $t0,0($sp)
addi $sp,$sp,4
la $t1,label_3_v1
sw $t0,0($t1)
lw $t0,0($sp)
addi $sp,$sp,4
la $t1,label_2_v0
sw $t0,0($t1)
lw $t0,0($sp)
addi $sp,$sp,4
la $t1,label_4_v2
sw $t0,0($t1)

