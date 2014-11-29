package com.example.app

import android.widget.ListView
import org.robolectric.Robolectric
import pl.polidea.robospock.RoboSpecification
import com.example.app.AppActivity

class AppActivitySpecification extends RoboSpecification {
    def "should have a ListView"() {
	    given:
	        def activity = Robolectric.buildActivity( AppActivity.class ).create().get()
        expect:
            activity.findViewById( R.id.testobject_app ) instanceof ListView
    }
}
