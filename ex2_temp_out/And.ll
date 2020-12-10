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
	%b = alloca i1
	%c = alloca i1
	%x = alloca i32
	%_0 = add i1 0, 0
	store i1 %_0, i1* %b
	%_1 = add i1 1, 0
	store i1 %_1, i1* %c
	%_2 = load i1, i1* %b
	br label %andcond0
andcond0:
	br i1 %_2, label %andcond1, label %andcond3
andcond1:
	%_3 = load i1, i1* %c
	br label %andcond4
andcond4:
	br i1 %_3, label %andcond5, label %andcond7
andcond5:
	%_4 = load i1, i1* %c
	br label %andcond6
andcond6:
	br label %andcond7
andcond7:
	%_5 = phi i1 [0, %andcond4], [%_4, %andcond6]
	br label %andcond2
andcond2:
	br label %andcond3
andcond3:
	%_6 = phi i1 [0, %andcond0], [%_5, %andcond2]
	br i1 %_6, label %if8, label %if9
if8:
	%_7 = add i32 0, 0
	store i32 %_7, i32* %x
	br label %if10
if9:
	%_8 = add i32 1, 0
	store i32 %_8, i32* %x
	br label %if10
if10:
	%_9 = load i32, i32* %x
	call void (i32) @print_int(i32 %_9)
	%_10 = add i32 0, 0
	ret i32 %_10
}

