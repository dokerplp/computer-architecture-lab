//В числах Фибоначчи четным будет каждое 3 число - четное, все остальные - нечетные
var x = 0
var y = 1
var t = 1
var max = 4000000
var stop = max
var acc = 0
var n = 3
while (stop) {
    stop = max
    acc += x
    n = 3
    while (n) {
        t = x
        x = y
        y += t
        n--
    }
    stop -= x
}
print(acc)