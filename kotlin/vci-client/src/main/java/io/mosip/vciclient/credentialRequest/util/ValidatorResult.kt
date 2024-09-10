package io.mosip.vciclient.credentialRequest.util

class ValidatorResult(var isValidated:Boolean= true) {
    val invalidFields: MutableList<String> = ArrayList()
    fun addInvalidField(invalidField:String){
        this.isValidated = false
        this.invalidFields.add(invalidField)
    }
}