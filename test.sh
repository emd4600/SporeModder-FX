# Initialize an empty string to store the result
solutions=""

# Use a while loop with redirection instead of piping to avoid subshell
while read dir; do
  while read file; do
    # Append the directory to the result string
    if [ ! -z "$solutions" ]; then
	  solutions="$solutions, "
    fi
    solutions="$solutions {\"directory\": \"$dir\", \"sln\": \"$file\"}"
  done < <(find "$dir" -mindepth 1 -maxdepth 1 -type f -name "*.sln" -printf "%f\n")
done < <(find "src/" -mindepth 1 -maxdepth 1 -type d)

solutions="matrix={ \"solution\": [ $solutions ] }"
echo "$solutions"