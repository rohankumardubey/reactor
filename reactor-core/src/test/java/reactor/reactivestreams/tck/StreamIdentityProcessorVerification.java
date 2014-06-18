/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.reactivestreams.tck;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.AbstractReactorTest;
import reactor.rx.Stream;
import reactor.rx.action.Action;
import reactor.rx.spec.Streams;
import reactor.util.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephane Maldini
 */
public class StreamIdentityProcessorVerification extends AbstractReactorTest {

	public Processor<Integer, Integer> createIdentityProcessor(int bufferSize) {
		return
				Action.<Integer>passthrough(env.getDispatcher("ringBuffer"))
						.env(env)
						.prefetch(bufferSize)
						.parallel(env.getDispatcher("workQueue"))
						.map(stream -> stream
										//.buffer()
										.map(integer -> integer)
										//.distinctUntilChanged()
										//.flatMap(Streams::defer)
										//.scan(Tuple2<Integer, Integer>::getT1)
										//.filter(integer -> integer >= 0)
										//.collect(1)
										//.last()
										//.<Integer>split()

						)
						.<Integer>merge()
						//.window(100)
						//.<Integer>split()
						.combine();
	}

	@org.junit.Test
	public void testIdentityProcessor() throws InterruptedException {

		final int elements = 10000;
		CountDownLatch latch = new CountDownLatch(elements+1);

		Processor<Integer, Integer> processor = createIdentityProcessor(Integer.MAX_VALUE);

		Stream<Integer> stream = Streams.defer(env);

		stream.subscribe(processor);
		processor.subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription s) {
				s.request(elements);
			}

			@Override
			public void onNext(Integer integer) {
				latch.countDown();
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onComplete() {
				System.out.println("completed!");
				latch.countDown();
			}
		});

		for (int i = 0; i < elements; i++) {
			stream.broadcastNext(i);
		}
		stream.broadcastComplete();

		latch.await(5,TimeUnit.SECONDS);
		System.out.println(stream.debug());
		long count = latch.getCount();
		Assert.state(latch.getCount() == 0, "Count > 0 : " + count);

	}
}
