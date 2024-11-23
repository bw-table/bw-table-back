import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Test
import org.junit.runner.RunWith
import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl

import static net.grinder.script.Grinder.grinder

@RunWith(GrinderRunner)
class TestRunner {

    public static GTest test
    public static HTTPRequest request

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test = new GTest(1, "예약 조회 테스트")
        request = new HTTPRequest()
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
    }

    @Test
    public void test() {
        // 특정 레스토랑의 예약 조회 요청
        def restaurantId = 1
        def date = "2024-11-22"
        def response = request.GET("http://localhost/api/reservations/restaurants/${restaurantId}?date=${date}")

        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Redirect received. Status code: " + response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
    }
}