int x = 0
int y = 1
int t = 1
int max = 4000000
int stop = max
int acc = 0
int n = 3
while (stop)
    stop = max
    acc = acc + x
    n = 3
    while (n)
        t = x
        x = y
        y = y + t
        n--
    end while
    stop = stop - x
end while
print(acc)