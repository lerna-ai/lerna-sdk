# Lerna Kotlin Multiplatform SDK
> Lerna Kotlin Multiplatform Mobile SDK

## Table of contents

- [Usage](USAGE.md#Usage)
  - [Use Lerna SDK](USAGE.md#use-lerna-sdk)
- [Utils](#utils)
  - [Extract usage readme for publication](#extract-usage-readme-for-publication)

## Usage

### Use Lerna SDK

Usage information can be found [here](USAGE.md)

## Utils

### Extract usage readme for publication

We use [mdpdf](https://www.npmjs.com/package/mdpdf) util to extract readme files as PDF.

#### Installation

To install the util globally to use from the command line.

```bash
npm install mdpdf -g
```

#### Usage

To save [USAGE.md](USAGE.md) file as `.pdf` run the following command

```bash
mdpdf ./USAGE.md
```
