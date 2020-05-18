package com.boyarsky.dapos.core.tx;

public class ErrorCodes {
    public static final ErrorCode OK = new ErrorCode(0);

    public static final String DEFAULT_VALIDATION = "Default Validation";
    public static final ErrorCode UNDEFINED_TYPE = new ErrorCode(DEFAULT_VALIDATION, -10);
    public static final ErrorCode SENDER_NOT_EXIST = new ErrorCode(DEFAULT_VALIDATION, -11);
    public static final ErrorCode NO_PUB_KEY_FOR_NEW_ACC = new ErrorCode(DEFAULT_VALIDATION, -12);
    public static final ErrorCode PUB_KEY_FOR_OLD_ACC = new ErrorCode(DEFAULT_VALIDATION, -13);
    public static final ErrorCode INCORRECT_PUB_KEY = new ErrorCode(DEFAULT_VALIDATION, -14);
    public static final ErrorCode FATAL_INCORRECT_PUB_KEY = new ErrorCode(DEFAULT_VALIDATION, -15);
    public static final ErrorCode WRONG_SIG_FORMAT = new ErrorCode(DEFAULT_VALIDATION, -16);
    public static final ErrorCode BAD_SIG = new ErrorCode(DEFAULT_VALIDATION, -17);
    public static final ErrorCode NOT_ENOUGH_MONEY = new ErrorCode(DEFAULT_VALIDATION, -18);
    public static final ErrorCode FEE_PROVIDER_NOT_EXIST = new ErrorCode(DEFAULT_VALIDATION, -19);
    public static final ErrorCode FEE_PROVIDER_NOT_ENABLED = new ErrorCode(DEFAULT_VALIDATION, -20);
    public static final ErrorCode FEE_PROVIDER_NOT_ENOUGH_FUNDS = new ErrorCode(DEFAULT_VALIDATION, -21);
    public static final ErrorCode FEE_PROVIDER_NOT_SUPPORTED_TX_TYPE = new ErrorCode(DEFAULT_VALIDATION, -22);
    public static final ErrorCode FEE_PROVIDER_NOT_ENOUGH_AVAILABLE_OPS = new ErrorCode(DEFAULT_VALIDATION, -23);
    public static final ErrorCode FEE_PROVIDER_NOT_ENOUGH_AVAIlABLE_FEE_LIMIT = new ErrorCode(DEFAULT_VALIDATION, -24);
    public static final ErrorCode FEE_PROVIDER_EXCEED_LIMIT_PER_OP = new ErrorCode(DEFAULT_VALIDATION, -25);
    public static final ErrorCode FEE_PROVIDER_EXCEED_TOTAL_LIMIT = new ErrorCode(DEFAULT_VALIDATION, -26);
    public static final ErrorCode FEE_PROVIDER_NOT_WHITELISTED_SENDER = new ErrorCode(DEFAULT_VALIDATION, -27);

    public static final String TX_PARSING = "Transaction Parsing";
    public static final ErrorCode GENERAL_PARSING_ERROR = new ErrorCode(TX_PARSING, 255);
    public static final ErrorCode DUPLICATE_MESSAGE_PARSING_ERROR = new ErrorCode(TX_PARSING, 250);

    public static final String VALIDATION = "Validation";
    public static final ErrorCode UNKNOWN_VALIDATION_ERROR = new ErrorCode(VALIDATION, -1000);
    public static final ErrorCode NOT_ENOUGH_GAS = new ErrorCode(VALIDATION, -100);
    public static final ErrorCode FAILED_GAS_CALC = new ErrorCode(VALIDATION, -101);
    public static final ErrorCode UNKNOWN_GAS_CALC_ERROR = new ErrorCode(VALIDATION, -102);

    public static final String MESSAGE_VALIDATION = "Message Validation";
    public static final ErrorCode RECIPIENT_NULL_DH = new ErrorCode(MESSAGE_VALIDATION, 42);
    public static final ErrorCode RECIPIENT_DB_MISSING = new ErrorCode(MESSAGE_VALIDATION, 43);
    public static final ErrorCode RECIPIENT_PK_MISSING = new ErrorCode(MESSAGE_VALIDATION, 44);

    public static final String FEE_PROV_VALIDATION = "Fee Provider";
    public static final ErrorCode RECIPIENT_EXIST = new ErrorCode(FEE_PROV_VALIDATION, 21);
    public static final ErrorCode ZERO_BALANCE = new ErrorCode(FEE_PROV_VALIDATION, 22);
    public static final ErrorCode DUPLICATE_FEE_ACCOUNT = new ErrorCode(FEE_PROV_VALIDATION, 23);
    public static final ErrorCode TOTAL_FEE_LESS_OP_FEE = new ErrorCode(FEE_PROV_VALIDATION, 24);

    public static final String VALIDATOR_CONTROL_VALIDATION = "Validator Control Validation";
    public static final ErrorCode NOT_FOUND_VALIDATOR = new ErrorCode(VALIDATOR_CONTROL_VALIDATION, 31);
    public static final ErrorCode ACCOUNT_NOT_ELIGIBLE_TO_CONTROL_VALIDATOR = new ErrorCode(VALIDATOR_CONTROL_VALIDATION, 32);
    public static final ErrorCode VALIDATOR_ALREADY_UP = new ErrorCode(VALIDATOR_CONTROL_VALIDATION, 33);
    public static final ErrorCode VALIDATOR_ALREADY_DOWN = new ErrorCode(VALIDATOR_CONTROL_VALIDATION, 34);
//    public static final ErrorCode TOTAL_FEE_LESS_OP_FEE = new ErrorCode(VALIDATOR_CONTROL_VALIDATION, 24);


    public static final String VOTE_VALIDATION = "Vote Validation";
    public static final ErrorCode VOTE_VALIDATOR_NOT_SPECIFIED = new ErrorCode(VOTE_VALIDATION, 71);
    public static final ErrorCode VOTE_VALIDATOR_NOT_FOUND = new ErrorCode(VOTE_VALIDATION, 72);
    public static final ErrorCode VOTE_VALIDATOR_DISABLED = new ErrorCode(VOTE_VALIDATION, 73);

    public static final String TX_HANDLING = "Transaction Handling";
    public static final ErrorCode HANDLING_ERROR = new ErrorCode(TX_HANDLING, 127);
    public static final ErrorCode UNKNOWN_HANDLING_ERROR = new ErrorCode(TX_HANDLING, 128);


}
