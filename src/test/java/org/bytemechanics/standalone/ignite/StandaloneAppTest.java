/*
 * Copyright 2018 Byte Mechanics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytemechanics.standalone.ignite;

import java.io.Closeable;

/**
 *
 * @author afarre
 */
public final class StandaloneAppTest implements Standalone,Runnable,Closeable{

	@Override
	public void run() {
		System.out.println("run");
	}

	@Override
	public void close() {
		System.out.println("close");
	}

	public static final void main(String... _args){
		System.out.println("initial Main");
		Standalone.ignite(() -> new StandaloneAppTest(),_args);
	}
}
