package com.example.libexplicit

import android.widget.ListView
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import com.example.libexplicit.LibExplicitActivity

class LibExplicitActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {
	    given:
	        def activity = Robolectric.buildActivity( LibExplicitActivity.class ).create().get()
        expect:
            activity.findViewById( R.id.testobject_libexplicit ) instanceof ListView
    }
}
