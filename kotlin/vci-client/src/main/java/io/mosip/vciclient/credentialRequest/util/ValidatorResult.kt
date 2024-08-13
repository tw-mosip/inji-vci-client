package io.mosip.vciclient.credentialRequest.util

class ValidatorResult(var isValidated:Boolean= true) {
    val invalidFields: MutableList<String> = ArrayList()
    fun addInvalidField(invalidField:String){
        this.invalidFields.add(invalidField)
    }
    fun setIsInvalid(){
        this.isValidated = false
    }
}