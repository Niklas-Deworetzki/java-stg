
class Example {
    // @volatile to make changes explicit in multithreading environment.
    @volatile var bitmap_0: Boolean = false
    var foo_0: Int = _

    private def foo_lzycompute(): Int = {
        // Lock the monitor of this instance.
        this.synchronized {
            if (!bitmap_0) {
                foo_0 = 42
                bitmap_0 = true
            }
        }
        return foo_0
    }

    def foo: Int = if (bitmap_0) foo_0 else foo_lzycompute()
}

