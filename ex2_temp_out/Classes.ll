@.Classes_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*)* @Classes.run to i8*)
]
@.Base_vtable = global [2 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @Base.set to i8*),
	i8* bitcast (i32 (i8*)* @Base.get to i8*)
]
@.Derived_vtable = global [2 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @Base.set to i8*),
	i8* bitcast (i32 (i8*)* @Base.get to i8*)
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
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Classes_vtable, i32 0, i32 0
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

define i32 @Classes.run(i8* %this) {
	%b = alloca i8*
	%d = alloca i8*

	%_0 = call i8* @calloc(i32 1, i32 12)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [2 x i8*], [2 x i8*]* @.Base_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%tmp1 = alloca i8*
	store i8* %_0, i8** %tmp1
	%_3 = load i8*, i8** %tmp1
	store i8* %_3, i8** %b
	%_4 = call i8* @calloc(i32 1, i32 12)
	%_5 = bitcast i8* %_4 to i8***
	%_6 = getelementptr [2 x i8*], [2 x i8*]* @.Derived_vtable, i32 0, i32 0
	store i8** %_6, i8*** %_5
	%tmp2 = alloca i8*
	store i8* %_4, i8** %tmp2
	%_7 = load i8*, i8** %tmp2

	store i8* %_7, i8** %d
	%_8 = load i8*, i8** %b

	%_9 = bitcast i8* %_8 to i8***
	%_10 = load i8**, i8*** %_9
	%_11 = getelementptr i8*, i8** %_10, i32 0
	%_12 = load i8*, i8** %_11
	%_13 = bitcast i8* %_12 to i32 (i8*, i32)*
	%_14 = add i32 1, 0
	%_15 = call i32 %_13(i8* %_8, i32 %_14)
	call void (i32) @print_int(i32 %_15)

	%_16 = load i8*, i8** %d
	store i8* %_16, i8** %b

	%_17 = load i8*, i8** %b
	%_18 = bitcast i8* %_17 to i8***
	%_19 = load i8**, i8*** %_18
	%_20 = getelementptr i8*, i8** %_19, i32 0
	%_21 = load i8*, i8** %_20
	%_22 = bitcast i8* %_21 to i32 (i8*, i32)*
	%_23 = add i32 3, 0
	%_24 = call i32 %_22(i8* %_17, i32 %_23)
	call void (i32) @print_int(i32 %_24)
	%_25 = load i8*, i8** %d
	%_26 = bitcast i8* %_25 to i8***
	%_27 = load i8**, i8*** %_26
	%_28 = getelementptr i8*, i8** %_27, i32 1
	%_29 = load i8*, i8** %_28
	%_30 = bitcast i8* %_29 to i32 (i8*)*
	%_31 = call i32 %_30(i8* %_25)
	call void (i32) @print_int(i32 %_31)
	%_32 = add i32 0, 0
	ret i32 %_32
}

define i32 @Base.set(i8* %this, i32 %.x) {
	%x = alloca i32
	store i32 %.x, i32* %x
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %x
	store i32 %_2, i32* %_1
	%_3 = getelementptr i8, i8* %this, i32 8
	%_4 = bitcast i8* %_3 to i32*
	%_5 = load i32, i32* %_4
	ret i32 %_5
}

define i32 @Base.get(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	ret i32 %_2
}

define i32 @Derived.set(i8* %this, i32 %.x) {
	%x = alloca i32
	store i32 %.x, i32* %x
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %x
	%_3 = mul i32 %_2, 2
	store i32 %_3, i32* %_1
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32*
	%_6 = load i32, i32* %_5
	ret i32 %_6
}

