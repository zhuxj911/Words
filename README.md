# Words
单词学习软件示例，学习AndroidX编程之用

远程仓库操作常用命令：
1. 在本地仓库添加一个远程仓库命令
   git remote add shortname url
   
    git remote add origin https://github.com/zhuxj911/Words

1. 显示远程仓库详细资料：
git remote -v | --verbose 列出详细信息，在每一个名字后面列出其远程url
git remote show origin

$ git remote -v
origin  https://github.com/zhuxj911/Words (fetch)
origin  https://github.com/zhuxj911/Words (push)


git remote set-url origin https://github.com/zhuxj911/Words
or
git remote rm origin 
git remote add origin https://github.com/zhuxj911/Words

3. 查看分支情况

$ git branch -r
  origin/HEAD -> origin/surfacepro
  origin/master
  origin/surfacepro

添加一个新的远程，抓取，并从它检出一个分支 
$ git remote add dev https://github.com/zhuxj911/Words

$ git remote
origin
staging
$ git fetch staging

命令git branch -a 查看所有分支

命令git branch -d Chapater8 可以删除本地分支（在主分支中）

命令git push origin --delete Chapater6   可以删除远程分支Chapater6  