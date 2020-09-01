package it.mormao.codepatterns;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;
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

		ArrayList<User> newArray = new ArrayList<>(list.size());

		IterableCollector<User> userCollector = IterableCollector.newInstance(list);

		//userCollector.sliceConsume((t1, t2) -> !t1.surname.equals(t2.surname), l -> System.out.println(l.stream().map(User::toString).collect(Collectors.joining(", ", "[", "]"))));
		userCollector.sliceConsume(2,
				  (t1, t2) -> !t1.surname.equals(t2.surname),
				  l -> System.out.println(l.stream().map(User::toString).collect(Collectors.joining(", ", "[", "]"))));

		userCollector.sliceConsume(2,(t1, t2) -> !t1.surname.equals(t2.surname), newArray::addAll);

		Assertions.assertEquals(newArray.size(), list.size());
		Assertions.assertArrayEquals(list.stream().sorted().toArray(), newArray.stream().sorted().toArray());
	}

	private static class User implements Comparable<User> {
		protected String name, surname;
		protected User(String name, String surname){
			this.name = name;
			this.surname = surname;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null || obj.getClass() != User.class)
				return false;
			User other = (User) obj;
			return Objects.equals(this.name, other.name) && Objects.equals(this.surname, other.surname);
		}

		@Override
		public String toString() {
			return "{name: \"" + name + "\"; surname: \"" + surname + "\" }";
		}

		@Override
		public int compareTo(final User user) {
			return user == null ? -1 : this.name.compareTo(user.name) + this.surname.compareTo(user.surname);
		}
	}
}
