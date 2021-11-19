
class Example {
    // Generated fields.
    var bitmap_0: Boolean = false
    var foo_0: Int = _

    // Evaluation function.
    private def foo_lzycompute(): Int = {
        if (!bitmap_0) {
            foo_0 = 42
            bitmap_0 = true
        }
        return foo_0
    }

    // Generated accessor.
    def foo: Int = if (bitmap_0) foo_0 else foo_lzycompute()
}
