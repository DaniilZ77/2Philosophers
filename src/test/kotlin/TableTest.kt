import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertTrue

class TableTest {
    private lateinit var table: Table

    @BeforeEach
    fun setUp() {
        var id = 0

        table =
            Table(
                Thinker(++id, 1, 2),
                Thinker(++id, 2, 3),
                Thinker(++id, 3, 4),
                Thinker(++id, 4, 5),
                Thinker(++id, 5, 1),
            )
    }

    @Test
    fun naiveTest() {
        val startLatch = CountDownLatch(1)

        val finishLatch = CountDownLatch(table.thinkers.size)

        thread {
            startLatch.await()
            table.eat {
                val leftFork = table.takeFork(leftForkId)
                log.debug("Thinker $id is taking the left fork with id: $leftForkId")

                val rightFork = table.takeFork(rightForkId)
                log.debug("Thinker $id is taking the right fork with id: $rightForkId")

                eat(leftFork, rightFork)

                table.releaseFork(rightForkId)
                log.debug("Thinker $id is releasing the right fork with id: $rightFork")

                table.releaseFork(leftForkId)
                log.debug("Thinker $id is releasing the left fork with id: $leftForkId")

                finishLatch.countDown()
            }
        }

        startLatch.countDown()
        val completedInTime = finishLatch.await(10, TimeUnit.SECONDS)

        assertTrue(!completedInTime, "Ожидался deadlock, но все операции были завершены вовремя")
    }

    @Test
    fun wiseTest() {
        val startLatch = CountDownLatch(1)

        val finishLatch = CountDownLatch(table.thinkers.size)

        thread {
            startLatch.await()
            table.eat {
                var leftForkIdCur = leftForkId
                var rightForkIdCur = rightForkId

                var isMaxIdThinker = false

                if (leftForkIdCur < rightForkId) {
                    leftForkIdCur = rightForkIdCur.also { rightForkIdCur = leftForkIdCur }
                    isMaxIdThinker = true
                }

                val leftForkName = if (isMaxIdThinker) "right" else "left"
                val rightForkName = if (isMaxIdThinker) "left" else "right"

                val leftFork = table.takeFork(leftForkIdCur)
                log.debug("Thinker $id is taking the $leftForkName fork with id: $leftForkIdCur")

                val rightFork = table.takeFork(rightForkIdCur)
                log.debug("Thinker $id is taking the $rightForkName fork with id: $rightForkIdCur")

                eat(leftFork, rightFork)

                table.releaseFork(rightForkIdCur)
                log.debug("Thinker $id is releasing the $rightForkName fork with id: $rightForkIdCur")

                table.releaseFork(leftForkIdCur)
                log.debug("Thinker $id is releasing the $leftForkName fork with id: $leftForkIdCur")

                finishLatch.countDown()
            }
        }

        startLatch.countDown()
        val completedInTime = finishLatch.await(10, TimeUnit.SECONDS)

        assertTrue(completedInTime, "Ожидалось, что все операции завершатся вовремя, но увы deadlock")
    }
}
