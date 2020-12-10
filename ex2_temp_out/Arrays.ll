@.Simple_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*)* @Simple.bar to i8*)
]


declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Simple_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%tmp0 = alloca i8*
	store i8* %_0, i8** %tmp0
	%_3 = load i8*, i8** %tmp0
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*)*
	%_9 = call i32 %_8(i8* %_3)
	call void (i32) @print_int(i32 %_9)
	ret i32 0
}

define i32 @Simple.bar(i8* %this) {
	%x = alloca i32*
	%_0 = add i32 2, 0
	%_1 = icmp slt i32 %_0, 0
	br i1 %_1, label %arr_alloc0, label %arr_alloc1
arr_alloc0:
	call void @throw_oob()
	br label %arr_alloc1
arr_alloc1:
	%_2 = add i32 %_0, 1
	%_3 = call i8* @calloc(i32 %_0, i32 4)
	%_4 = bitcast i8* %_3 to i32*
	store i32 %_0, i32* %_4
	store i32* %_4, i32** %x
	%_5 = load i32*, i32** %x
	%_6 = add i32 0, 0
	%_7 = icmp slt i32 %_6, 0
	br i1 %_7, label %arr_alloc2, label %arr_alloc3
arr_alloc2:
	call void @throw_oob()
	br label %arr_alloc3
arr_alloc3:
	%_8 = getelementptr i32, i32* %_5, i32 0
	%_9 = load i32, i32* %_8
	%_10 = icmp slt i32 %_9, %_6
	br i1 %_10, label %arr_alloc4, label %arr_alloc5
arr_alloc4:
	call void @throw_oob()
	br label %arr_alloc5
arr_alloc5:
	%_11 = add i32 %_6, 1
	%_12 = getelementptr i32, i32* %_5, i32 %_11
	%_13 = add i32 1, 0
	store i32 %_13, i32* %_12
	%_14 = load i32*, i32** %x
	%_15 = add i32 1, 0
	%_16 = icmp slt i32 %_15, 0
	br i1 %_16, label %arr_alloc6, label %arr_alloc7
arr_alloc6:
	call void @throw_oob()
	br label %arr_alloc7
arr_alloc7:
	%_17 = getelementptr i32, i32* %_14, i32 0
	%_18 = load i32, i32* %_17
	%_19 = icmp slt i32 %_18, %_15
	br i1 %_19, label %arr_alloc8, label %arr_alloc9
arr_alloc8:
	call void @throw_oob()
	br label %arr_alloc9
arr_alloc9:
	%_20 = add i32 %_15, 1
	%_21 = getelementptr i32, i32* %_14, i32 %_20
	%_22 = add i32 2, 0
	store i32 %_22, i32* %_21
	%_23 = load i32*, i32** %x
	%_24 = add i32 0, 0
	%_25 = icmp slt i32 %_24, 0
	br i1 %_25, label %arr_alloc10, label %arr_alloc11
arr_alloc10:
	call void @throw_oob()
	br label %arr_alloc11
arr_alloc11:
	%_26 = getelementptr i32, i32* %_23, i32 0
	%_27 = load i32, i32* %_26
	%_28 = icmp slt i32 %_27, %_24
	br i1 %_28, label %arr_alloc12, label %arr_alloc13
arr_alloc12:
	call void @throw_oob()
	br label %arr_alloc13
arr_alloc13:
	%_29 = add i32 %_24, 1
	%_30 = getelementptr i32, i32* %_23, i32 %_29
	%_31 = load i32, i32* %_30
	%_32 = load i32*, i32** %x
	%_33 = add i32 1, 0
	%_34 = icmp slt i32 %_33, 0
	br i1 %_34, label %arr_alloc14, label %arr_alloc15
arr_alloc14:
	call void @throw_oob()
	br label %arr_alloc15
arr_alloc15:
	%_35 = getelementptr i32, i32* %_32, i32 0
	%_36 = load i32, i32* %_35
	%_37 = icmp slt i32 %_36, %_33
	br i1 %_37, label %arr_alloc16, label %arr_alloc17
arr_alloc16:
	call void @throw_oob()
	br label %arr_alloc17
arr_alloc17:
	%_38 = add i32 %_33, 1
	%_39 = getelementptr i32, i32* %_32, i32 %_38
	%_40 = load i32, i32* %_39
	%_41 = add i32 %_31, %_40
	call void (i32) @print_int(i32 %_41)
	%_42 = add i32 0, 0
	ret i32 %_42
}

