package com.example.appinv

import android.widget.ListView
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import com.example.appinv.AppInvActivity

class AppInvActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {
	    given:
	        def mainActivity = Robolectric.buildActivity( AppInvActivity.class ).create().get()
        expect:
            mainActivity.findViewById( R.id.testobject_appinv ) instanceof ListView
    }
}

