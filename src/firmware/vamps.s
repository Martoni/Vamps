	.option nopic
	.text
	.align	2

begin:
    lui     x5, 0x7af
	addi	x1,sp,0xab
    lui     x1, 0xfee
	jal     x2, begin 
    lui     x5, 0x7af
	addi	x1,sp,0xab
    lui     x1, 0xfee

