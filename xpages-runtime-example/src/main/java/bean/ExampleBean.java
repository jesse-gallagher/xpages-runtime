package bean;

import java.io.Serializable;

public class ExampleBean implements Serializable {
	private static final long serialVersionUID = 1L;

	public String getFoo() {
		return "Bean says " + System.currentTimeMillis();
	}

}
