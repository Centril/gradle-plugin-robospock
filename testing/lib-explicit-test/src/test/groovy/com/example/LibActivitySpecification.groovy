package com.example

import android.widget.ListView
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import com.example.LibExplicitActivity

class LibExplicitActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {
	    given:
	        def activity = Robolectric.buildActivity( LibExplicitActivity.class ).create().get()
        expect:
            activity.findViewById( R.id.testobject ) instanceof ListView
    }
}
