package com.example

import android.widget.ListView
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import com.example.AppInvActivity

class AppInvActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {
	    given:
	        def mainActivity = Robolectric.buildActivity( AppInvActivity.class ).create().get()
        expect:
            mainActivity.findViewById( R.id.testobject ) instanceof ListView
    }
}

