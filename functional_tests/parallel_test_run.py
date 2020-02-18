from subprocess import Popen, PIPE
import sys
import webcolors

chrome_cmd = 'export BROWSER=chrome && python test.py'
firefox_cmd = 'export BROWSER=firefox && python test.py'
opera_cmd = 'export BROWSER=opera && python test.py'
safari_cmd = 'export BROWSER=safari && python test.py'
ie_cmd = 'export BROWSER="internet explorer" && python test.py'


chrome_cmd_short = 'export BROWSER=chrome && python test.py NewVisitorTest'
firefox_cmd_short = 'export BROWSER=firefox && python test.py NewVisitorTest'
# OPERA NOT WORKING ON 62 !

# all_tests = [chrome_cmd, firefox_cmd, opera_cmd, safari_cmd, ie_cmd]
# all_tests = [chrome_cmd, firefox_cmd, opera_cmd, safari_cmd]
all_tests = [chrome_cmd, firefox_cmd, opera_cmd, ie_cmd]
# all_tests = [chrome_cmd, firefox_cmd] * 4
# all_tests = [chrome_cmd, firefox_cmd]
# all_tests = [chrome_cmd]
# all_tests = [firefox_cmd]
# all_tests = [opera_cmd]
# all_tests = [safari_cmd]
# all_tests = [ie_cmd]

# all_tests = [firefox_cmd_short, chrome_cmd_short]

processes = []
return_codes = []

# TODO. This is running synchronous. I need to somehow collect all the error and pop it at the end.
# Other option is to look into better testing framework
# Or some sort of jenkins plugin.

for a_test_run in all_tests:
    process = Popen(a_test_run, shell=True)
    # process = Popen(a_test_run, shell=True, stdout=PIPE)
    processes.append(process)
    # out, _ = process.communicate()
    # if process.returncode != 0:
    #     sys.exit(1)
    # print(process.returncode)
    # print(out)

# Wait for child process to terminate. Set and return returncode attribute.
# for i in range(len(all_tests)):
#     processes[i].wait()
#     print(f'poll: {processes[i].poll()}')

# Wait for child process to terminate. Set and return returncode attribute.
# print("Before wait")
for process in processes:
    process.wait()
    return_codes.append(process.poll())
    # print(process)
    # print(process.__dict__)
    # print(f'poll: {process.poll()}')

# print("After wait")

# print(f'return_codes: {return_codes}')
# print(f'sum: {sum(return_codes)}')

return_codes_sum = sum(return_codes)
if return_codes_sum > 0:
    print(f'Failed tests total: {return_codes_sum}.')
    sys.exit(1)
elif return_codes_sum == 0:
    print(f'All tests passed.')
    sys.exit(return_codes_sum)
