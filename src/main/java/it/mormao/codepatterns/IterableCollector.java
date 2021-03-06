package it.mormao.codepatterns;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A utility class, whose purpose is to eliminate boilerplate code and repetitions needed to accumulate elements of a list and operate on portions of them
 * with bulk operations. E.g.: sending files to a service in block of 100 or less, or bulk-inserting record from an ordered list until a property changes
 * @param <T>
 * @author mormao
 */
@SuppressWarnings("unused")
public class IterableCollector<T> {
	Iterable<T> it;

	protected IterableCollector(Iterable<T> it){
		this.it = it;
	}

	public static<T> IterableCollector<T> newInstance(Iterable<T> it){
		return new IterableCollector<>(it);
	}

	public static<T> IterableCollector<T> newInstance(T[] arr){
		return new IterableCollector<>(new ArrayList<>(Arrays.asList(arr)));
	}

	/**
	 * Execute the "accept" operation of the consumer on each subsequent couple of elements T1 and T2 such that comparator.test(T1, T2) returns true
	 * @param comparator the boolean predicate which states if the consumer has to be executed or not
	 * @param consumer the consumer that accept(T1, T2)
	 */
	public void consume(BiPredicate<T, T> comparator, BiConsumer<T, T> consumer){
		Iterator<T> init = it.iterator();
		if(init.hasNext()) {
			T start = init.next();
			T next;
			boolean lastConsumed;
			while(init.hasNext()){
				next = init.next();
				lastConsumed = false;
				if(comparator.test(start, next)) {
					consumer.accept(start, next);
					lastConsumed = true;
				}

				if(init.hasNext())
					start = next;
				else if(!lastConsumed)
					consumer.accept(start, next);
			}
		}
	}

	/**
	 * Execute the "accept" operation of the consumer on each subsequent couple of elements T1 and T2 such that comparator.test(T1, T2) returns true
	 * or the limit of element specified by "splitEvery" parameter has been reached
	 * @param splitEvery maximum number of elements every which the "accept" operation has to be executed, regardless of the BiPredicate.test result
	 * @param comparator the boolean predicate which states if the consumer has to be executed or not
	 * @param consumer the consumer that accept(T1, T2)
	 */
	public void consume(final int splitEvery, BiPredicate<T, T> comparator, BiConsumer<T, T> consumer){
		if(splitEvery > 0) {
			final AtomicInteger count = new AtomicInteger(0);
			consume((t1, t2) -> count.incrementAndGet() >= splitEvery || comparator.test(t1,t2), (t1, t2) -> {
				count.set(0);
				consumer.accept(t1, t2);
			});
		} else
			consume(comparator, consumer);
	}

	/**
	 * Execute the "accept" operation of the consumer each time
	 * or the limit of element specified by "splitEvery" parameter has been reached
	 * @param splitEvery maximum number of elements every which the "accept" operation has to be executed, regardless of the BiPredicate.test result
	 * @param consumer the consumer that accept(T1, T2)
	 */
	public void consumeEvery(final int splitEvery, BiConsumer<T, T> consumer){
		if(splitEvery > 0) {
			AtomicInteger count = new AtomicInteger(0);
			this.consume((t1, t2) -> count.incrementAndGet() >= splitEvery, (t1,t2) -> {
				count.set(0);
				consumer.accept(t1, t2);
			});
		} else
			consumeEvery(1, consumer);
	}

	/**
	 * Accumulate a certain number of elements, checking each couple of subsequent elements T1 and T2 until splitCondition.test(T1,T2) returns true,
	 * then "accept(accumulatedList)"
	 * @param splitCondition the boolean predicate which states if the consumer has to be executed or not
	 * @param consumer the consumer that accept(accumulatedList)
	 */
	public void sliceConsume(BiPredicate<T, T> splitCondition, Consumer<List<T>> consumer){
		Iterator<T> init = it.iterator();
		if(init.hasNext()) {
			List<T> accumulator = new ArrayList<>();
			T start = init.next();
			T next;
			if(!init.hasNext()) {
				accumulator.add(start);
				consumer.accept(accumulator);
			} else {
				do {
					accumulator.add(start);
					next = init.next();

					if (splitCondition.test(start, next)) {
						consumer.accept(accumulator);
						accumulator = new ArrayList<>();
						if(!init.hasNext())
							accumulator.add(next);
					}

					if (init.hasNext())
						start = next;
					else if (!accumulator.isEmpty())
						consumer.accept(accumulator);
				} while (init.hasNext());
			}
		}
	}

	/**
	 * Accumulate a certain number of elements, checking each couple of subsequent elements T1 and T2 until splitCondition.test(T1,T2) returns true
	 * or the limit of element specified by "splitEvery" parameter has been reached
	 * @param splitEvery maximum number of elements every which the "accept" operation has to be executed, regardless of the BiPredicate.test result
	 * @param splitCondition the boolean predicate which states if the consumer has to be executed or not
	 * @param consumer the consumer that accept(accumulatedList)
	 */
	public void sliceConsume(final int splitEvery, BiPredicate<T, T> splitCondition, Consumer<List<T>> consumer) {
		if(splitEvery > 0) {
			final AtomicInteger count = new AtomicInteger(0);
			sliceConsume((t1, t2) -> count.incrementAndGet() >= splitEvery || splitCondition.test(t1,t2), l -> {
				count.set(0);
				consumer.accept(l);
			});
		} else
			sliceConsume(splitCondition, consumer);
	}

	/**
	 * Accumulate a certain number of elements, until mapper.apply(t1) != mapper.apply(t2)
	 * @param mapper a function which maps an element of type T to any element of any type
	 * @param consumer the consumer that accept(accumulatedList)
	 */
	public void sliceConsume(Function<T,?> mapper, Consumer<List<T>> consumer){
		sliceConsume((t1, t2) -> !Objects.equals(mapper.apply(t1), mapper.apply(t2)), consumer);
	}

	/**
	 * Accumulate a certain number of elements, until mapper.apply(t1) != mapper.apply(t2) or the limit specified by "splitEvery"
	 * @param splitEvery maximum number of elements every which the "accept" operation has to be executed, regardless of the BiPredicate.test result
	 * @param mapper the boolean predicate which states if the consumer has to be executed or not
	 * @param consumer the consumer that accept(accumulatedList)
	 */
	public void sliceConsume(final int splitEvery, Function<T,?> mapper, Consumer<List<T>> consumer){
		if(splitEvery > 0) {
			final AtomicInteger count = new AtomicInteger(0);
			sliceConsume((t1, t2) -> count.incrementAndGet() >= splitEvery || !Objects.equals(mapper.apply(t1), mapper.apply(t2)), l -> {
				count.set(0);
				consumer.accept(l);
			});
		} else
			sliceConsume(mapper, consumer);
	}

	/**
	 * Accumulate a certain number of elements, until the limit specified by "splitEvery" parameter has been reached or the list is terminated
	 * then "accept(accumulatedList)"
	 * @param splitEvery maximum number of elements every which the "accept" operation has to be executed, regardless of the BiPredicate.test result
	 * @param consumer the consumer that accept(accumulatedList)
	 */
	public void sliceConsumeEvery(final int splitEvery, Consumer<List<T>> consumer){
		if(splitEvery > 0) {
			final AtomicInteger count = new AtomicInteger(0);
			sliceConsume((t1, t2) -> count.incrementAndGet() >= splitEvery, l -> {
				count.set(0);
				consumer.accept(l);
			});
		} else
			sliceConsumeEvery(1, consumer);
	}
}
