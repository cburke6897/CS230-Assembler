void main(String[] args) {
    StringBuilder machineCode = new StringBuilder(); //Where converted machine code will be built

    int currentAddress = 0; //Current address in decimal
    HashMap<String, Integer> symbols = new HashMap<>(); //Where symbols and address will be stored

    HashMap<String, String> instructionMap = getInstructionMap();

    List<String> operandlessInstructions = Arrays.asList("STOP", "ASLA", "ASRA"); //The only instructions without operands

    if (args.length == 0) {
        IO.println("No argument given");
        return;
    }

    if (args.length > 1) {
        IO.println("Too many arguments given");
        return;
    }

    if (!args[0].matches(".+\\.pep")) {
        IO.println("Invalid argument format (not 'x.pep')");
        return;
    }

    Scanner scanner;
    try { //Get .pep file
        FileReader reader = new FileReader(args[0]);
        scanner = new Scanner(reader);
    } catch (FileNotFoundException e) {
        IO.println("File not found");
        return;
    }

    while (scanner.hasNextLine()) { //Goes through .pep file
        String[] lineSegments = scanner.nextLine().trim().replaceAll(" +", " ").split(" ");
        if (lineSegments.length == 1 && lineSegments[0].isEmpty()) {
            continue;
        }

        if (lineSegments[0].endsWith(":")) { //Checks if the first element is a symbol
            symbols.put(lineSegments[0].substring(0, lineSegments[0].length() - 1), currentAddress); //Records symbol and address
            lineSegments = Arrays.copyOfRange(lineSegments, 1, lineSegments.length); //Removes the symbol from the lineSegments array
        }

        String instruction = lineSegments[0];

        if (instruction.equals(".END")) {
            break;
        } //Ends conversion if instruction is .END

        String machineInstruction = instructionMap.get(instruction);
        machineCode.append(machineInstruction);

        if (machineInstruction.length() == 1) { //If machine instruction is only one character checks the addressing mode
            if (lineSegments[2].equals("i")) {
                machineCode.append("0 ");
            } else {
                machineCode.append("1 ");
            }
        }

        if (!operandlessInstructions.contains(instruction)) { //Checks if the instruction doesn't have an operand
            String operand = lineSegments[1]; //Gets the operand

            if (operand.endsWith(",")) {
                operand = operand.replace(",", ""); //Removes trailing comma
            } else {
                IO.println("Invalid operand");
                return;
            }

            if (instruction.equals("BRNE")) { //Checks if the instruction is BRNE
                if (lineSegments[2].equals("d")) { //Checks if the addressing mode is direct, and if so gives an error message
                    IO.println("Invalid addressing mode for BRNE instruction");
                    return;
                }

                String symbolAddress = String.format("%4s", Integer.toHexString(symbols.get(operand)).toUpperCase()).replace(' ', '0'); //Gets the int symbol address as a hex string

                machineCode.append(symbolAddress, 0, 2).append(" ").append(symbolAddress, 2, 4).append(" "); //Appends symbols address with space in the middle

            } else {
                if (operand.startsWith("0x")) { //Checks if operand starts with '0x'
                    operand = operand.replace("0x", ""); //Removes '0x'
                } else {
                    IO.println("Invalid operand");
                    return;
                }

                operand = String.format("%4s", operand).replace(' ', '0'); //Fills the operand with 0s

                machineCode.append(operand, 0, 2).append(" ").append(operand, 2, 4).append(" "); //Appends operand minus 0x with space in the middle
            }
        }

        currentAddress += 3; //Increments the current address by 3
    }

    IO.println(machineCode);
}

private static HashMap<String, String> getInstructionMap() {
    HashMap<String, String> instructionMap = new HashMap<>(); //Instructions map with machine code
    instructionMap.put("STBA", "F");
    instructionMap.put("LDBA", "D");
    instructionMap.put("STWA", "E");
    instructionMap.put("LDWA", "C");
    instructionMap.put("ADDA", "6");
    instructionMap.put("ASLA", "0A ");
    instructionMap.put("ASRA", "0C ");
    instructionMap.put("STOP", "00 ");
    instructionMap.put("CPBA", "B");
    instructionMap.put("BRNE", "1A ");
    return instructionMap;
}
