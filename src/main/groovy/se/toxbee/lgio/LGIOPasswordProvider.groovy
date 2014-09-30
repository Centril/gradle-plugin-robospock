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
package se.toxbee.lgio

import javax.swing.JPasswordField
import javax.swing.JTextField

/**
 * LGIOPasswordProvider uses Console.readPassword or a JPasswordField.
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class LGIOPasswordProvider extends LGIOInputProvider.Base {
	@Override
	JTextField generateInput() { new JPasswordField() }

	@Override
	String getData() {
		def char[] pwd = ((JPasswordField) input).getPassword()
		def retr = new String( pwd )
		// Clear pwd for security.
		for ( int i = 0; i < pwd.length; ++i ) {
			pwd[i] = 0;
		}
		return retr
	}

	@Override
	String readConsole( Console c, String print ) { new String( c.readPassword( print ) ) }
}