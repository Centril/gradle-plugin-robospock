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

import javax.swing.JTextField;

/**
 * LGIOTextProvider uses Console.readLine or a JTextField.
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class LGIOTextProvider extends LGIOInputProvider.Base {
	@Override
	JTextField generateInput() { new JTextField() }

	@Override
	String getData() { input.getText() }

	@Override
	String readConsole( Console c, String print ) { c.readLine( print ) }
}