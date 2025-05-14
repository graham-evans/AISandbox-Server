# Contributing to AI Sandbox

Whether you want to create an additional simulation, or just improve our existing code, we'd love to include your contributions into the next release of the Sandbox. However, code will only be excepted in the form of a pull request to the Github repository.

Before we can accept your contribution you will need to confirm you agree that:

1. If a simulation is based on existing intellectual property (IP), you own the rights to this (including graphics / documentation / code) and are willing (and able) to include this IP in an open source product, licenced under the GPLv3 licence.
2. If you are submitting code on behalf of a company, you are empowered to do this and bind them to releasing these changes under the GPLv3.
3. You (our your company) retains the copyright of your code, but agree to licence any contributions you make to the AI Sandbox project under the terms of the GPLv3. This allows us to include your contributions in the project and release it as part of our GPLv3 licenced product, but does not stop you from (seperatly) including your own code in another project even if this is released under a different licence.

Please reach out to the maintainers via a Github issue to discuss agreeing to these terms.

## Technical Considerations and code quality

Contributions are expected to follow the following guidelines, which can be checked via the inbuilt code analysis tools (PMD and CheckStyle):

1. Code should be written to use the existing framework as described in this documentation, large scale changes which require updating other simulations should be discussed with the maintainers before submission.
2. Dependencies should be kept to an absolute minimum. Talk to the maintainers before starting to develop any change that would require updating the Gradle dependencies.
3. Code quality is measured with the ```pmdMain``` and ```pmdTest``` Gradle tasks. There should be no **High** warnings, and the ```@SupressWarnings``` annotation should be only be used after careful evaluation of the issue.
4. Source code should be formatted using the "Google Standard" set of rules [Documented here](https://google.github.io/styleguide/javaguide.html).
5. There must be a GPLv3 licence notice at the start of each Java source file. This is enforced with the ```checkstyleMain``` and ```checkstyleTest``` Gradle tasks.

