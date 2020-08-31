package it.mormao.codepatterns;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class IteratorCollectorTest {
	@Test
	public void testList(){
		ArrayList<User> list = new ArrayList<>();
		list.add(new User("mario", "rossi"));
		list.add(new User("giovanni", "rossi"));
		list.add(new User("roberto", "rossi"));
		list.add(new User("marco", "rossi"));
		list.add(new User("ugo", "rossi"));
		list.add(new User("marco", "bianchi"));
		list.add(new User("manuel", "bianchi"));
		list.add(new User("francesca", "bianchi"));

		IterableCollector<User> userCollector = IterableCollector.newInstance(list);

		//userCollector.sliceConsume((t1, t2) -> !t1.surname.equals(t2.surname), l -> System.out.println(l.stream().map(User::toString).collect(Collectors.joining(", ", "[", "]"))));
		userCollector.sliceConsume(2, (t1, t2) -> !t1.surname.equals(t2.surname), l -> System.out.println(l.stream().map(User::toString).collect(Collectors.joining(", ", "[", "]"))));
	}

	private static class User {
		protected String name, surname;
		protected User(String name, String surname){
			this.name = name;
			this.surname = surname;
		}

		@Override
		public String toString() {
			return "{name: \"" + name + "\"; surname: \"" + surname + "\" }";
		}
	}
}
