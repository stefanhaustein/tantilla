package org.kobjects.wasm

const val I32_CONST = 0x41;
const val I64_CONST = 0x42;
const val F32_CONST = 0x43;
const val F64_CONST = 0x44;

const val DROP = 0x1a;

class Interpreter {
    // TODO: Consider using https://github.com/chmp/ktwasm

    fun run(opcodes: ByteArray) {
        var pc = 0
        var op = opcodes[pc]
        when (op) {



        }


    }

}