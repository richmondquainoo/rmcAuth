
def sum_of_squares(nums):
    return sum(x**2 for x in nums if x >= 0)


num_test_cases = int(input())

# Define a function for each test case
def test_case(_):
    num_integers = int(input())
    integers = list(map(int, input().split()))
    return sum_of_squares(integers)

# calculate the results using map() and list comprehension
results = list(map(test_case, range(num_test_cases)))

# Print the results for all test cases without blank lines
print("\n".join(map(str, results)))
