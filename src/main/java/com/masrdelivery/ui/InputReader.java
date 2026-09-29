package com.masrdelivery.ui;

import com.masrdelivery.exception.NullEntityException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;

public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        if (scanner == null) {
            throw new NullEntityException("Scanner");
        }
        this.scanner = scanner;
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    public String readNonEmptyStringOutOfPool (String prompt, String[] pool) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty() && Arrays.stream(pool).anyMatch(input::equals)) {
                return input;
            }
            System.out.println("Input must be one of the following: " + Arrays.toString(pool) + ". Please try again.");
        }
    }

    public String readOptionalString(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine().trim();
    }

    public char readNonEmptyChar(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (input.length() == 1) {
                return input.charAt(0);
            }
            System.out.println("Input must be a single character. Please try again.");
        }
    }

    public char readNonEmptyCharOutOfPool(String prompt, char[] pool) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (input.length() == 1) {
                char c = input.charAt(0);
                if (new String(pool).indexOf(c) != -1) {
                    return c;
                }
                System.out.println("Input must be one of the following: " + Arrays.toString(pool) + ". Please try again.");
            }
            System.out.println("Input must be a single character. Please try again.");
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        while (true) {
            try {
                System.out.println(prompt);
                int number = Integer.parseInt(scanner.nextLine().trim());
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("Input must be between " + min + " and " + max + ". Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                int input = Integer.parseInt(scanner.nextLine().trim());
                if (input > 0) {
                    return input;
                }
                System.out.println("Input must be a positive integer. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                double input = Double.parseDouble(scanner.nextLine().trim());
                if (input > 0) {
                    return input;
                }
                System.out.println("Input must be positive. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public double readDoubleInRange(String prompt, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        while (true) {
            try {
                System.out.println(prompt);
                double input = Double.parseDouble(scanner.nextLine().trim());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.println("Input must be between " + min + " and " + max + ". Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                BigDecimal value = new BigDecimal(input);
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    return value;
                }
                System.out.println("Input must be positive. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public BigDecimal readBigDecimalInRange(String prompt, BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            throw new NullEntityException("BigDecimal");
        }
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min must be less than max");
        }
        while (true) {
            System.out.println(prompt);
            try {
                BigDecimal input = new BigDecimal(scanner.nextLine().trim());
                if (input.compareTo(min) >= 0 && input.compareTo(max) <= 0) {
                    return input;
                }
                System.out.println("Input must be between " + min + " and " + max + ". Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        System.out.println(prompt);
        for (int i = 0; i < constants.length; i++) {
            System.out.printf("%d. %s\n", i + 1, constants[i].name());
        }
        int choice = readIntInRange("Enter your choice", 1, constants.length);
        return constants[choice - 1];
    }
}
