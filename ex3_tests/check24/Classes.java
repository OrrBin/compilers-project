class Main {
	public static void main(String[] args) {
		System.out.println(new Classes().run());
	}
}

class Classes {
	public int run() {
		Base b;
		Derived b;

  		b = new Base();
 		b = new Derived();

		System.out.println(b.set(1));

		b = b;

		System.out.println(b.set(3));

		System.out.println(b.get());

		return 0;
	}
}

class Base {
	int data;

	public int set(int x, int x) {
		data = x;

		return data;
	}

	public int get() {
		return data;
	}
}

class Derived extends Base {
	public int set(int x) {
		data = x * 2;

		return data;
	}
}
