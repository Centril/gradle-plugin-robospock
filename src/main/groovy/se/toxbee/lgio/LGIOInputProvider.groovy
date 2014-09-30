/*
 * Copyright 2014 toxbee.se
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.toxbee.lgio;

import javax.swing.JTextField;

/**
 * LGIOInputProvider provides a unified way for LGIOPlugin
 * to get the same input data from both GUI & console.
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
interface LGIOInputProvider {
	static abstract class Base implements LGIOInputProvider {
		abstract JTextField generateInput()
		JTextField input
		JTextField getInput() { input == null ? input = generateInput() : input }
	}

	/**
	 * Returns a string from the JTextField returned by {@link #getInput()}.
	 *
	 * @return the string representation.
	 */
	String getData()

	/**
	 * Reads String from console.
	 *
	 * @param c the console.
	 * @param print the exact value to print out to console, don't alter or format.
	 * @return the string representation.
	 */
	String readConsole( Console c, String print )

	/**
	 * Lazy loads an input JTextField to use for GUI.
	 *
	 * @return tye GUI component.
	 */
	JTextField getInput()
}