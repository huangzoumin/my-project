# a = 100
# if a>=0:
#     print(a)
# else:
#     print(-a)
#
# print('i am ok中文测试')
# print('\\\n\\')
#
# print('Hi,%s,you have $%d' %('Benjamin',1000))

# classmates = ['huang','zhang','wang']
# print(classmates)
# classmates.insert(0,'first')
# len = len(classmates)
# print(len)
# print(classmates[0])
# print(classmates[1])
# print(classmates[2])
#
# t = (1,2,['a','b'])
# print(t)
# t[2][0] = 's'
# print(t)

# sum = 0
# for i in (range(4)):
#     sum += i
# print(sum)

# tmp = {'huang':1,'zhang':2}#dict
# tmp['wang'] = 3
# print(classmates[1:2])

# class Animal(object):
#     def run(self):
#         print('Animal is running')
#
# class Cat(Animal):
#     def run(self):
#         print('Cat is running')
#     def setAge(self,age):
#         self.age = age
#     def getAge(self):
#         print('it s  '+str(self.age)+' years old!!')
#
# class Dog(Animal):
#     def run(self):
#         print('Dog is running')
#
# def run_twice(Animal):
#     Animal.run()
#     Animal.run()
# c = Cat()
# c.setAge(3)
# c.getAge()
# run_twice(Animal())
# run_twice(c)
# run_twice(Dog())
#
# print(type(Cat()))

try:
    print('try...')
    r = 10 / 0
    print('result:', r)
except ZeroDivisionError as e:
    print('except:', e)
finally:
    print('finally...')
print('END')