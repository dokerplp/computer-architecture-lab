
int x = 1 + 2 + 3 + 4 + 5
print(x)
int y = x + x + 34
print(y)
int z = y - x + 1 + y
z = z + z
print(z)
x = z - x + y
y = x - y + z
z = y - z + x
print(x)
print(y)
print(z)
