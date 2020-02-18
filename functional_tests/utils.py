import re

def hex_to_rgb(hex_color):
    color = hex_color.lstrip('#')
    rgb_color = tuple(int(color[i:i+2], 16) for i in (0, 2 ,4))
    return rgb_color


def rgb_to_hex(rgb):
    hex_color = "".join([format(val, '02X') for val in rgb])
    return f"#{hex_color}"


def rgb_to_rgba(rgb, opacity):
    """
    Takes rgb tuple and opacity between 0-1 and return rgba tuple.
    """
    color_rgba = rgb + (opacity,)
    return color_rgba

def rgb_to_rgba_string(rgb, opacity):
    """
    Takes rgb tuple and opacity between 0-1 and return rgba tuple. Returns string version.
    """
    color_rgba = rgb + (opacity,)
    return f"rgba{color_rgba}"


def rbga_to_rgb(rgba):
    """
    Takes in tuple of (red, green, blue, opacity).
    Assumes background colour is white.
    """
    BGColor = (255,255,255)

    r = ((1 - rgba[3]) * BGColor[0]) + (rgba[3] * rgba[0])
    g = ((1 - rgba[3]) * BGColor[1]) + (rgba[3] * rgba[1])
    b = ((1 - rgba[3]) * BGColor[2]) + (rgba[3] * rgba[2])
    rgb = (r,g,b)
    return rgb


def is_rgb(rgb: str) -> bool:
    """
    Checks if it is in RGB format.
    :param rgb:
    :return:
    """
    x = re.match('rgb?(\(\s*\d+\s*,\s*\d+\s*,\s*\d+)(?:\s*,.+?)?\)', rgb)
    if x:
        return True
    if not x:
        return False


def standardize_colour(color_str: str) -> str:
    """
    Checks if colour is in rgb and returns rgba version
    :param rgb:
    :return:
    """

    if is_rgb(color_str):
        rgb_list_str = re.findall('[0-9]+', color_str)
        rgb_list_int = list(map(int, rgb_list_str))
        rgb_tuple_str = tuple(rgb_list_int)
        return rgb_to_rgba_string(rgb_tuple_str, 1)
    if not is_rgb(color_str):
        return color_str


if __name__ == '__main__':
    # a = is_rgb('rgb(123, 123, 123)')
    # b = is_rgb('rgba(123, 123, 123, 1)')
    # print(a)
    # print(b)
    # print(standardize_colour('rgb(123, 123, 123)'))
    print(standardize_colour('rgba(123, 123, 123, 1)'))
    print(rgb_to_rgba_string('rgb(123, 123, 123)', 1))
