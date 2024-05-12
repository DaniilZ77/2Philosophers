import org.slf4j.LoggerFactory
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

val log = LoggerFactory.getLogger("Thinkers")

interface Identifiable {
    val id: Int
}

data class Fork(override val id: Int, val mutex: Lock = ReentrantLock()) : Identifiable

interface IThinker : Identifiable {
    // Have to be executed for actual eat
    fun eat(
        leftFork: Fork,
        rightFork: Fork,
    ) {
        Thread.sleep(1000)
    }
}

data class Thinker(
    override val id: Int,
    val leftForkId: Int,
    val rightForkId: Int,
) : IThinker

class Table(vararg elements: Thinker) {
    val forks: List<Fork>
    val thinkers: List<Thinker>

    init {
        thinkers = elements.toList()

        forks =
            thinkers
                .flatMap { setOf(it.leftForkId, it.rightForkId) }
                .toSet()
                .map { Fork(it) }
                .toList()
    }

    fun takeFork(forkId: Int): Fork {
        val fork = requireNotNull(forks.find { it.id == forkId })
        fork.mutex.lock()
        return fork
    }

    fun releaseFork(forkId: Int): Fork {
        val fork = requireNotNull(forks.find { it.id == forkId })
        fork.mutex.unlock()
        return fork
    }

    fun eat(howToEat: Thinker.() -> Unit) {
        thinkers.forEach { thread { it.howToEat() } }
    }
}

fun main() {
    // naive implementation that causes deadlock
    var id = 0
    val table =
        Table(
            Thinker(++id, 1, 2),
            Thinker(++id, 2, 3),
            Thinker(++id, 3, 4),
            Thinker(++id, 4, 5),
            Thinker(++id, 5, 1),
        )
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
    }
}
