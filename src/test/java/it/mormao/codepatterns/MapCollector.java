package it.mormao.codepatterns;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class MapCollector<T,U> {
	private final Map<T, U> map;
	private MapCollector(Map<T,U> map){
		this.map = map;
	}

	public static<T,U> MapCollector<T,U> newInstance(Map<T,U> map){
		return new MapCollector<>(map);
	}

	public void sliceConsume(BiPredicate<Map.Entry<T,U>, Map.Entry<T,U>> splitCondition, Consumer<Map<T,U>> consumer){
		if(map == null || map.isEmpty())
			return;
		Iterator<Map.Entry<T,U>> it = map.entrySet().iterator();
		if(it.hasNext()) {
			Map.Entry<T,U> start = it.next();
			Map.Entry<T,U> next;
			HashMap<T,U> accumulator = new HashMap<>();
			if(!it.hasNext()){
				accumulator.put(start.getKey(), start.getValue());
				consumer.accept(accumulator);
			} else {
				do {
					accumulator.put(start.getKey(), start.getValue());
					next = it.next();

					if (splitCondition.test(start, next)) {
						consumer.accept(accumulator);
						accumulator = new HashMap<>();
						if(!it.hasNext())
							accumulator.put(next.getKey(), next.getValue());
					}

					if (it.hasNext())
						start = next;
					else if (!accumulator.isEmpty())
						consumer.accept(accumulator);
				} while (it.hasNext());
			}
		}
	}

	public void sliceConsume(final int splitEvery, BiPredicate<Map.Entry<T,U>, Map.Entry<T,U>> splitCondition, Consumer<Map<T,U>> consumer){
		if(splitEvery > 0) {
			final AtomicInteger count = new AtomicInteger(0);
			sliceConsume((t1, t2) -> count.incrementAndGet() >= splitEvery || splitCondition.test(t1,t2), l -> {
				count.set(0);
				consumer.accept(l);
			});
		} else
			sliceConsume(splitCondition, consumer);
	}

	public void sliceConsumeEvery(final int splitEvery, Consumer<Map<T,U>> consumer){
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
