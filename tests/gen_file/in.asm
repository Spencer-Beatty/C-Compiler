.data

.text
j main

.text
main:
li v4,1
li v5,1
li v6,1
add v5, v6, v5
add v4, v5, v4
li v1,1
li v2,1
li v3,1
add v3, v4, v3
add v2, v3, v2
add v1, v2, v1

add $a0, v1, $zero
li $v0, 1
syscall