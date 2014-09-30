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

import groovy.swing.SwingBuilder
import org.gradle.api.InvalidUserDataException

import java.awt.Color
import java.awt.GridBagConstraints

/**
 * See LGIOPlugin.groovy
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class LGIOPluginExtension {
	/*
	 * Public API:
	 */

	/**
	 * Prints a string to STDOUT.
	 * Equivalent of System.out.println(...)
	 *
	 * @param message the string to print.
	 */
	void println( String message ) { System.out.println( message ) }

	/**
	 * Reads a line from either console or GUI.
	 *
	 * @param message message to display to client.
	 * @return the read line, possibly empty.
	 */
	String readLine( String message ) { readImpl( message, new LGIOTextProvider() ) }

	/**
	 * Reads a password from either console or GUI.
	 *
	 * @param message message to display to client.
	 * @return the read password, possibly empty.
	 */
	String readPassword( String message ) { readImpl( message, new LGIOPasswordProvider() ) }

	/**
	 * Reads a line from either console or GUI.
	 * If client provides an empty line, an exception is thrown.
	 *
	 * @param message message to display to client.
	 * @param onFail optional on-failure message to provide.
	 * @return the read line, never empty.
	 */
	String readLineReq( String message, String onFail = "Input given was empty" ) {
		testRequired( readLine( message ), onFail )
	}

	/**
	 * Reads a password from either console or GUI.
	 * If client provides an empty password, an exception is thrown.
	 *
	 * @param message message to display to client.
	 * @param onFail optional on-failure message to provide.
	 * @return the read password, never empty.
	 */
	String readPasswordReq( String message, String onFail = "Password given was empty" ) {
		testRequired( readPassword( message ), onFail )
	}

	/**
	 * Reads something from either console or GUI.
	 * This is a low level method, only for advanced use.
	 *
	 * @param message message to display to client.
	 * @return the read string, possibly empty.
	 */
	String readImpl( String message, LGIOInputProvider ip ) {
		// Use console if available, otherwise use GUI/javax.swing.
		Console c = System.console()
		return c == null ? readGUI( message, ip ) : ip.readConsole( c, "\n" + message + ':' );
	}

	/*
	 * "Private" implementation:
	 */

	String testRequired( String input, String onFail ) {
		if ( input == null || input.size() <= 0 ) {
			throw new InvalidUserDataException( onFail )
		}
		return input
	}

	private String readGUI( String message, LGIOInputProvider ip ) {
		def retr = ''
		SwingBuilder b = new SwingBuilder()
		b.edt {
			dialog( modal: true, // Otherwise the build will continue running before you closed the dialog
			        title: message,
			        alwaysOnTop: true, resizable: false,
			        locationRelativeTo: null,
			        pack: true, show: true
			) {
				panel( border: emptyBorder( 20 ), background: Color.WHITE ) {
					gridBagLayout()

					label( text: message + ':', constraints: gbc(
							gridx: 0, gridy: 0,
							gridwidth: GridBagConstraints.REMAINDER,
							fill: GridBagConstraints.HORIZONTAL,
							insets: [0, 0, 10, 0]
					) )

					widget( ip.getInput(), margin: [5, 8, 5, 8], constraints: gbc(
							gridx: 0, gridy: 1,
							gridwidth: GridBagConstraints.REMAINDER,
							fill: GridBagConstraints.BOTH,
							insets: [0, 0, 10, 0]
					) )

					button( defaultButton: true, text: 'OK',
					        constraints: gbc(
							        gridx: 0, gridy: 2,
							        gridwidth: 1,
							        anchor: GridBagConstraints.EAST,
							        fill: GridBagConstraints.VERTICAL,
							        insets: [0, 0, 0, 0]
					        ),
					        actionPerformed: {
						        retr = ip.getData();
						        dispose(); // Close dialog
					        }
					)
				}
			}
		}

		return retr;
	}
}