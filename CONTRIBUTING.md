# Contributing to AI Sandbox

Thanks for your interest in contributing! This document explains the rules for
submitting code and other content to AI Sandbox. By opening a pull request you
agree to everything below, so please read it before you start.

## License

AI Sandbox is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
All contributions are accepted under the same license. If your change includes
material that cannot be distributed under GPL-3.0, we cannot accept it.

## Developer Certificate of Origin (DCO)

To keep the project's provenance clean, every commit must be signed off under
the **Developer Certificate of Origin**. The sign-off is your certification
that you wrote the contribution, or otherwise have the right to submit it under
the project's license. We do not use a copyright-assignment agreement — you keep
the copyright to your work.

### How to sign off

Add a `Signed-off-by` line to each commit, using your real name and an email
address where you can be reached:

```
Signed-off-by: Jane Developer <jane@example.com>
```

Git can add this automatically with the `-s` flag (configure `user.name` and
`user.email` first):

```
git commit -s -m "Your commit message"
```

If you forgot to sign off your most recent commit:

```
git commit --amend -s --no-edit
git push --force-with-lease
```

For multiple commits, rebase with sign-off:

```
git rebase --signoff HEAD~<number-of-commits>
git push --force-with-lease
```

Pull requests are automatically checked, and commits without a valid sign-off
will block the merge until corrected.

### The full text of the DCO

```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.


Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

## Originality and rights

In addition to the DCO sign-off, by submitting a contribution you confirm that:

- The work is your own, or you have the explicit right to submit it under
  GPL-3.0. Do not submit code, assets, or other material copied from a source
  you do not have permission to relicense. This includes reimplementing a game,
  tool, or other work whose code or assets you do not own the rights to.
- The contribution does **not** include material owned by your employer or a
  client, **unless** you have their written permission to release it under
  GPL-3.0. If your employment agreement assigns your work to a company, it is
  your responsibility to clear the contribution before submitting it.
- Any third-party material you include (libraries, snippets, assets) is
  compatible with GPL-3.0, and you have noted its source and license.

If you are unsure whether you have the right to submit something, please ask
before opening a pull request.

## Use of AI-generated content

AI Sandbox has a specific policy on content produced with the help of
generative AI tools.

### Code: allowed, when human-directed

You may use AI coding assistants to help write contributions, provided that:

- The work is **human-directed** — you guide, review, and understand every
  change. You are responsible for the code as if you had written it by hand.
- You have reviewed the output and are confident it does not reproduce
  copyrighted code verbatim or otherwise violate a third party's rights.
- Your DCO sign-off still applies in full: you certify you have the right to
  submit the code under GPL-3.0.

In short: AI may assist you, but it does not author your contribution — you do.
Submissions that appear to be unreviewed AI output may be rejected.

### Art and visual assets: not accepted

We do **not** accept AI-generated artwork or visual assets of any kind. This
includes images, icons, textures, sprites, logos, and similar material produced
by generative image models.

All visual assets must be either:

- Original work created by a human contributor, or
- Existing assets under a license compatible with GPL-3.0, with the source and
  license clearly documented.

The provenance and copyright status of AI-generated images is unsettled and
carries more risk than we are willing to take on for this project.

## Submitting a pull request

1. Fork the repository and create a branch for your change.
2. Make your changes, with clear commit messages, each signed off (`-s`).
3. Make sure any tests pass and the project builds.
4. Open a pull request describing what you changed and why.
5. Respond to review feedback; a maintainer will merge once the change and the
   DCO check are approved.

## Reporting a rights problem

If you believe content in this project infringes your rights, please open an
issue so it can be reviewed and removed if necessary.

---

Thanks for helping make AI Sandbox better.
